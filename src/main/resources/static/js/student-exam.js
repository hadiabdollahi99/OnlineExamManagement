class StudentExam {
    constructor() {
        this.currentQuestionIndex = 0;
        this.questions = [];
        this.participationId = null;
        this.timer = null;
        this.autoSave = null;
        this.remainingTime = 0;
        this.isSubmitting = false;
    }

    initialize(data) {
        this.questions = data.questions || [];
        this.participationId = data.participationId || 0;
        this.remainingTime = data.remainingTime || 0;

        console.log('StudentExam initialized:', {
            questionsCount: this.questions.length,
            participationId: this.participationId,
            remainingTime: this.remainingTime
        });

        this.createQuestionNavigation();
        this.showQuestion(this.currentQuestionIndex);
        this.startTimer();
        this.startAutoSave();
        this.setupEventListeners();
    }

    createQuestionNavigation() {
        const navContainer = document.getElementById('question-nav');
        if (!navContainer) return;

        navContainer.innerHTML = '';

        this.questions.forEach((question, index) => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'nav-btn';
            btn.textContent = index + 1;
            btn.onclick = () => this.showQuestion(index);

            // بررسی آیا پاسخ داده شده است
            if (question.studentAnswer || question.selectedOptionId) {
                btn.classList.add('answered');
            }

            navContainer.appendChild(btn);
        });
    }

    showQuestion(index) {
        this.currentQuestionIndex = index;

        // به‌روزرسانی دکمه‌های ناوبری
        document.querySelectorAll('.nav-btn').forEach((btn, i) => {
            btn.classList.remove('active');
            if (i === index) {
                btn.classList.add('active');
            }
        });

        const question = this.questions[index];
        const container = document.getElementById('question-container');

        if (!container) return;

        let html = `
            <div class="question-title">سوال ${index + 1}: ${this.escapeHtml(question.question.title)}</div>
            <div class="question-text">${this.escapeHtml(question.question.questionText)}</div>
        `;

        if (question.isMultipleChoice) {
            html += `<div class="options-container">`;

            question.options.forEach(option => {
                const isSelected = option.id === question.selectedOptionId;
                html += `
                    <div class="option-item ${isSelected ? 'selected' : ''}" 
                         onclick="studentExam.selectOption(${option.id})">
                        ${this.escapeHtml(option.optionText)}
                    </div>
                `;
            });

            html += `</div>`;

        } else if (question.isDescriptive) {
            html += `
                <textarea class="descriptive-answer" 
                          oninput="studentExam.saveDescriptiveAnswer(this.value)"
                          placeholder="پاسخ خود را اینجا بنویسید...">${this.escapeHtml(question.studentAnswer || '')}</textarea>
            `;
        }

        container.innerHTML = html;

        // به‌روزرسانی دکمه‌های navigation
        this.updateNavigationButtons();
    }

    selectOption(optionId) {
        const question = this.questions[this.currentQuestionIndex];
        question.selectedOptionId = optionId;

        // نمایش بصری
        document.querySelectorAll('.option-item').forEach(item => {
            item.classList.remove('selected');
        });
        event.target.classList.add('selected');

        // ذخیره پاسخ
        this.saveCurrentAnswer();

        // به‌روزرسانی ناوبری
        document.querySelectorAll('.nav-btn')[this.currentQuestionIndex].classList.add('answered');
    }


    saveDescriptiveAnswer(answerText) {
        const question = this.questions[this.currentQuestionIndex];
        question.studentAnswer = answerText;

        const formData = new FormData();
        formData.append('questionId', question.id);
        formData.append('answerText', answerText); // اضافه کردن answerText
        // ... بقیه کد مربوط به ارسال فرم

        this.questions[this.currentQuestionIndex].studentAnswer = answerText;
        this.saveCurrentAnswer();

        // به‌روزرسانی ناوبری
        document.querySelectorAll('.nav-btn')[this.currentQuestionIndex].classList.add('answered');
    }

    saveCurrentAnswer() {
        const question = this.questions[this.currentQuestionIndex];

        if (this.autoSave) {
            this.autoSave.setAnswer(question.id, {
                selectedOptionId: question.selectedOptionId,
                answerText: question.studentAnswer
            });
        }
    }

    startTimer() {
        this.timer = new ExamTimer(
            this.remainingTime,
            (remainingSeconds) => this.updateTimerDisplay(remainingSeconds),
            () => this.submitExamAuto()
        );

        this.timer.start();

        // هشدارها
        setTimeout(() => {
            if (this.remainingTime <= 300) { // 5 دقیقه
                this.showWarning('⚠ فقط 5 دقیقه تا پایان آزمون باقی مانده است!');
            }
        }, (this.remainingTime - 300) * 1000);

        setTimeout(() => {
            if (this.remainingTime <= 60) { // 1 دقیقه
                this.showWarning('⚠ فقط 1 دقیقه تا پایان آزمون باقی مانده است!');
            }
        }, (this.remainingTime - 60) * 1000);
    }

    updateTimerDisplay(remainingSeconds) {
        const timerDisplay = document.getElementById('timer-display');
        if (!timerDisplay) return;

        const hours = Math.floor(remainingSeconds / 3600);
        const minutes = Math.floor((remainingSeconds % 3600) / 60);
        const seconds = remainingSeconds % 60;

        timerDisplay.textContent =
            `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;

        // تغییر رنگ در 5 دقیقه پایانی
        if (remainingSeconds <= 300) {
            timerDisplay.style.background = '#dc3545';
        }
    }

    startAutoSave() {
        if (!this.participationId) return;

        this.autoSave = new ExamAutoSave(this.participationId, 30000); // هر 30 ثانیه
        this.autoSave.start();
    }

    updateNavigationButtons() {
        const prevBtn = document.getElementById('prev-btn');
        const nextBtn = document.getElementById('next-btn');
        const submitBtn = document.getElementById('submit-btn');

        if (prevBtn) prevBtn.disabled = this.currentQuestionIndex === 0;
        if (nextBtn) nextBtn.disabled = this.currentQuestionIndex === this.questions.length - 1;

        // نمایش دکمه submit در سوال آخر
        if (nextBtn && submitBtn) {
            if (this.currentQuestionIndex === this.questions.length - 1) {
                nextBtn.style.display = 'none';
                submitBtn.style.display = 'inline-block';
            } else {
                nextBtn.style.display = 'inline-block';
                submitBtn.style.display = 'none';
            }
        }
    }

    prevQuestion() {
        if (this.currentQuestionIndex > 0) {
            this.showQuestion(this.currentQuestionIndex - 1);
        }
    }

    nextQuestion() {
        if (this.currentQuestionIndex < this.questions.length - 1) {
            this.showQuestion(this.currentQuestionIndex + 1);
        }
    }

    submitExamAuto() {
        if (this.isSubmitting) return;

        this.showWarning('زمان آزمون به پایان رسیده است. آزمون به طور خودکار ارسال می‌شود...');

        setTimeout(() => {
            this.submitExam();
        }, 3000);
    }

    submitExam() {
        if (this.isSubmitting) return;

        if (confirm('آیا از ارسال نهایی آزمون اطمینان دارید؟ پس از ارسال نمی‌توانید تغییری ایجاد کنید.')) {
            this.isSubmitting = true;

            // ذخیره همه پاسخ‌ها قبل از ارسال
            if (this.autoSave) {
                this.autoSave.saveAllAnswers().then(() => {
                    document.getElementById('exam-form').submit();
                });
            } else {
                document.getElementById('exam-form').submit();
            }
        }
    }

    setupEventListeners() {
        // جلوگیری از خروج تصادفی
        window.addEventListener('beforeunload', (e) => {
            if (this.remainingTime > 0 && !this.isSubmitting) {
                e.preventDefault();
                e.returnValue = 'اگر از صفحه خارج شوید، پاسخ‌های شما ذخیره می‌شود اما ممکن است زمان از دست برود. آیا مطمئن هستید؟';
            }
        });

        // رویدادهای کلیک
        document.addEventListener('click', (e) => {
            if (e.target.id === 'prev-btn') this.prevQuestion();
            if (e.target.id === 'next-btn') this.nextQuestion();
            if (e.target.id === 'submit-btn') this.submitExam();
            if (e.target.id === 'save-btn') {
                if (this.autoSave) {
                    this.autoSave.saveAllAnswers();
                }
            }
        });
    }

    showWarning(message) {
        alert(message);
    }

    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// ایجاد نمونه جهانی
const studentExam = new StudentExam();